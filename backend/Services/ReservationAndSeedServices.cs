using Microsoft.AspNetCore.Identity;
using MongoDB.Driver;
using SmartSolarMicrogrid.Api.Configuration;
using SmartSolarMicrogrid.Api.Data;
using SmartSolarMicrogrid.Api.DTOs;
using SmartSolarMicrogrid.Api.Models;

namespace SmartSolarMicrogrid.Api.Services;

public interface IReservationService
{
    Task<EnergyReservation> CreateAsync(string nic, ReservationRequest r);
    Task<EnergyReservation> CreateBookingAsync(string nic, BookingRequest r);
    Task<EnergyReservation> UpdateAsync(string id, string nic, ReservationUpdateRequest r);
    Task CancelAsync(string id, string nic);
    Task<List<EnergyReservation>> QueryAsync(string nic, string? state);
    Task<EnergyReservation> VerifyAsync(string token);
    Task<EnergyReservation> FinalizeAsync(string id, string operatorName);
}

public sealed class ReservationService(MongoContext db) : IReservationService
{
    private static bool Notice(DateTime start) => start >= DateTime.UtcNow.AddHours(12);

    // NEW: used by BookingsController. Matches what the Android app sends.
    public async Task<EnergyReservation> CreateBookingAsync(string nic, BookingRequest r)
    {
        if (r.EnergyKwh <= 0)
            throw new InvalidOperationException("Energy amount must be greater than 0.");

        if (string.IsNullOrWhiteSpace(r.NodeId))
            throw new InvalidOperationException("Node is required.");

        if (string.IsNullOrWhiteSpace(r.SlotTime))
            throw new InvalidOperationException("Time slot is required.");

        // The app sends the date as UTC (e.g. 2026-10-11T18:30:00Z).
        // Allow one day of slack so timezone differences don't reject today's bookings.
        if (r.SlotDate.ToUniversalTime() < DateTime.UtcNow.Date.AddDays(-1))
            throw new InvalidOperationException("Booking date cannot be in the past.");

        var res = new EnergyReservation
        {
            ProsumerNic = nic,                       // always from the token, never from the body
            EnergyKwh = r.EnergyKwh,
            NodeId = r.NodeId.Trim(),
            SlotDate = r.SlotDate.ToUniversalTime(),
            SlotTime = r.SlotTime.Trim(),
            Notes = string.IsNullOrWhiteSpace(r.Notes) ? null : r.Notes.Trim()
        };

        await db.Reservations.InsertOneAsync(res);
        return res;
    }

    // Original slot-based booking (still used by ReservationsController)
    public async Task<EnergyReservation> CreateAsync(string nic, ReservationRequest r)
    {
        var slot = await db.Slots
            .Find(x => x.Id == r.SlotId && x.IsActive)
            .FirstOrDefaultAsync() ?? throw new KeyNotFoundException("Available slot not found.");

        if (slot.StartTime > DateTime.UtcNow.AddDays(7) || slot.StartTime < DateTime.UtcNow)
            throw new InvalidOperationException("Bookings must be within the next 7 days.");

        if (r.EnergyKwh > slot.AvailableKwh)
            throw new InvalidOperationException("Insufficient energy available.");

        var res = new EnergyReservation
        {
            ProsumerNic = nic,
            StationId = slot.StationId,
            SlotId = slot.Id,
            EnergyKwh = r.EnergyKwh
        };

        slot.AvailableKwh -= r.EnergyKwh;

        await db.Reservations.InsertOneAsync(res);
        await db.Slots.ReplaceOneAsync(x => x.Id == slot.Id, slot);

        return res;
    }

    public async Task<EnergyReservation> UpdateAsync(string id, string nic, ReservationUpdateRequest r)
    {
        var res = await Owned(id, nic);
        var slot = await db.Slots
            .Find(x => x.Id == res.SlotId)
            .FirstOrDefaultAsync() ?? throw new KeyNotFoundException("Slot not found.");

        if (!Notice(slot.StartTime))
            throw new InvalidOperationException("Updates require at least 12 hours notice.");

        var difference = r.EnergyKwh - res.EnergyKwh;
        if (difference > slot.AvailableKwh)
            throw new InvalidOperationException("Insufficient energy available.");

        slot.AvailableKwh -= difference;
        res.EnergyKwh = r.EnergyKwh;
        res.UpdatedAt = DateTime.UtcNow;

        await db.Slots.ReplaceOneAsync(x => x.Id == slot.Id, slot);
        await db.Reservations.ReplaceOneAsync(x => x.Id == id, res);

        return res;
    }

    public async Task CancelAsync(string id, string nic)
    {
        var res = await Owned(id, nic);

        // Bookings made through the app have no slot, so just cancel the reservation.
        if (string.IsNullOrEmpty(res.SlotId))
        {
            res.Status = ReservationStatus.Cancelled;
            res.UpdatedAt = DateTime.UtcNow;
            await db.Reservations.ReplaceOneAsync(x => x.Id == id, res);
            return;
        }

        var slot = await db.Slots
            .Find(x => x.Id == res.SlotId)
            .FirstOrDefaultAsync() ?? throw new KeyNotFoundException("Slot not found.");

        if (!Notice(slot.StartTime))
            throw new InvalidOperationException("Cancellation requires at least 12 hours notice.");

        res.Status = ReservationStatus.Cancelled;
        res.UpdatedAt = DateTime.UtcNow;
        slot.AvailableKwh += res.EnergyKwh;

        await db.Reservations.ReplaceOneAsync(x => x.Id == id, res);
        await db.Slots.ReplaceOneAsync(x => x.Id == slot.Id, slot);
    }

    public Task<List<EnergyReservation>> QueryAsync(string nic, string? state)
    {
        var f = Builders<EnergyReservation>.Filter.Eq(x => x.ProsumerNic, nic);

        if (Enum.TryParse<ReservationStatus>(state, true, out var s))
        {
            f &= Builders<EnergyReservation>.Filter.Eq(x => x.Status, s);
        }

        return db.Reservations
            .Find(f)
            .SortByDescending(x => x.CreatedAt)
            .ToListAsync();
    }

    public async Task<EnergyReservation> VerifyAsync(string token)
    {
        return await db.Reservations
            .Find(x => x.QrToken == token && x.Status != ReservationStatus.Cancelled)
            .FirstOrDefaultAsync() ?? throw new KeyNotFoundException("Valid reservation not found.");
    }

    public async Task<EnergyReservation> FinalizeAsync(string id, string op)
    {
        var r = await db.Reservations
            .Find(x => x.Id == id)
            .FirstOrDefaultAsync() ?? throw new KeyNotFoundException("Reservation not found.");

        if (r.Status == ReservationStatus.Cancelled || r.Status == ReservationStatus.Completed)
            throw new InvalidOperationException("Reservation cannot be finalized.");

        r.Status = ReservationStatus.Completed;
        r.FinalizedAt = DateTime.UtcNow;
        r.FinalizedBy = op;
        r.UpdatedAt = DateTime.UtcNow;

        await db.Reservations.ReplaceOneAsync(x => x.Id == id, r);

        return r;
    }

    private async Task<EnergyReservation> Owned(string id, string nic)
    {
        var r = await db.Reservations
            .Find(x => x.Id == id && x.ProsumerNic == nic)
            .FirstOrDefaultAsync() ?? throw new KeyNotFoundException("Reservation not found.");

        if (r.Status == ReservationStatus.Cancelled || r.Status == ReservationStatus.Completed)
            throw new InvalidOperationException("Reservation is no longer editable.");

        return r;
    }
}

public sealed class DatabaseSeeder(MongoContext db)
{
    public async Task SeedAsync()
    {
        await db.Users.Indexes.CreateOneAsync(
            new CreateIndexModel<User>(
                Builders<User>.IndexKeys.Ascending(x => x.Username),
                new CreateIndexOptions { Unique = true }
            )
        );

        await db.Prosumers.Indexes.CreateOneAsync(
            new CreateIndexModel<Prosumer>(
                Builders<Prosumer>.IndexKeys.Ascending(x => x.Nic),
                new CreateIndexOptions { Unique = true }
            )
        );

        await db.Slots.Indexes.CreateOneAsync(
            new CreateIndexModel<EnergyBookingSlot>(
                Builders<EnergyBookingSlot>.IndexKeys.Ascending(x => x.StationId).Ascending(x => x.StartTime)
            )
        );

        await db.Reservations.Indexes.CreateOneAsync(
            new CreateIndexModel<EnergyReservation>(
                Builders<EnergyReservation>.IndexKeys.Ascending(x => x.ProsumerNic).Ascending(x => x.Status)
            )
        );

        if (await db.Users.Find(_ => true).AnyAsync())
            return;

        var h = new PasswordHasher<User>();
        var usersToSeed = new[]
        {
            new User { Username = "backoffice", Role = Roles.Backoffice },
            new User { Username = "operator", Role = Roles.GridOperator },
            new User { Username = "199012345678", Role = Roles.Prosumer, ProsumerNic = "199012345678" },
            new User { Username = "h@gmail.com", Role = Roles.Prosumer, ProsumerNic = "200316410731" }
        };

        foreach (var u in usersToSeed)
        {
            u.PasswordHash = h.HashPassword(u, "Admin@123");
            await db.Users.InsertOneAsync(u);
        }

        var p = new Prosumer
        {
            Nic = "199012345678",
            FullName = "Sample Prosumer",
            Email = "prosumer@example.com",
            Phone = "0771234567",
            Address = "Colombo"
        };
        await db.Prosumers.InsertOneAsync(p);

        var station = new SolarStationInfo
        {
            Name = "Colombo Solar Hub",
            Location = "Colombo",
            Latitude = 6.9271,
            Longitude = 79.8612,
            CapacityKwh = 500,
            BatteryStorageSlots = 12
        };
        await db.Stations.InsertOneAsync(station);

        await db.Slots.InsertOneAsync(new EnergyBookingSlot
        {
            StationId = station.Id,
            StartTime = DateTime.UtcNow.Date.AddDays(1).AddHours(10),
            EndTime = DateTime.UtcNow.Date.AddDays(1).AddHours(12),
            AvailableKwh = 100,
            PricePerKwh = 42.50m
        });
    }
}