using MongoDB.Driver;
using SmartSolarMicrogrid.Api.Data;
using SmartSolarMicrogrid.Api.DTOs;
using SmartSolarMicrogrid.Api.Models;

namespace SmartSolarMicrogrid.Api.Services;

public interface IStationService
{
    Task<List<SolarStationInfo>> GetAsync(bool activeOnly = true);
    Task<SolarStationInfo> CreateAsync(StationRequest r);
    Task<SolarStationInfo> UpdateAsync(string id, StationRequest r);
    Task DeactivateAsync(string id);
}

public sealed class StationService(MongoContext db) : IStationService
{
    public Task<List<SolarStationInfo>> GetAsync(bool activeOnly = true)
    {
        return db.Stations
            .Find(x => !activeOnly || x.IsActive)
            .ToListAsync();
    }

    public async Task<SolarStationInfo> CreateAsync(StationRequest r)
    {
        var station = new SolarStationInfo
        {
            Name = r.Name,
            Location = r.Location,
            Latitude = r.Latitude,
            Longitude = r.Longitude,
            CapacityKwh = r.CapacityKwh,
            BatteryStorageSlots = r.BatteryStorageSlots
        };

        await db.Stations.InsertOneAsync(station);
        return station;
    }

    public async Task<SolarStationInfo> UpdateAsync(string id, StationRequest r)
    {
        var station = await db.Stations
            .Find(x => x.Id == id)
            .FirstOrDefaultAsync() ?? throw new KeyNotFoundException("Station not found.");

        station.Name = r.Name;
        station.Location = r.Location;
        station.Latitude = r.Latitude;
        station.Longitude = r.Longitude;
        station.CapacityKwh = r.CapacityKwh;
        station.BatteryStorageSlots = r.BatteryStorageSlots;
        station.UpdatedAt = DateTime.UtcNow;

        await db.Stations.ReplaceOneAsync(x => x.Id == id, station);
        return station;
    }

    public async Task DeactivateAsync(string id)
    {
        var hasActiveReservations = await db.Reservations
            .Find(x => x.StationId == id && (x.Status == ReservationStatus.Pending || x.Status == ReservationStatus.Confirmed))
            .AnyAsync();

        if (hasActiveReservations)
            throw new InvalidOperationException("Station has active reservations.");

        var station = await db.Stations
            .Find(x => x.Id == id)
            .FirstOrDefaultAsync() ?? throw new KeyNotFoundException("Station not found.");

        station.IsActive = false;
        await db.Stations.ReplaceOneAsync(x => x.Id == id, station);
    }
}

public interface ISlotService
{
    Task<List<EnergyBookingSlot>> GetAsync(string? stationId);
    Task<EnergyBookingSlot> CreateAsync(SlotRequest r);
    Task<EnergyBookingSlot> UpdateAsync(string id, SlotRequest r);
    Task DeleteAsync(string id);
}

public sealed class SlotService(MongoContext db) : ISlotService
{
    public Task<List<EnergyBookingSlot>> GetAsync(string? stationId)
    {
        return db.Slots
            .Find(x => x.IsActive && (stationId == null || x.StationId == stationId) && x.EndTime > DateTime.UtcNow)
            .ToListAsync();
    }

    public async Task<EnergyBookingSlot> CreateAsync(SlotRequest r)
    {
        if (r.EndTime <= r.StartTime)
            throw new InvalidOperationException("End time must be after start time.");

        var stationExists = await db.Stations
            .Find(x => x.Id == r.StationId && x.IsActive)
            .FirstOrDefaultAsync() != null;

        if (!stationExists)
            throw new InvalidOperationException("Active station not found.");

        var slot = new EnergyBookingSlot
        {
            StationId = r.StationId,
            StartTime = r.StartTime.ToUniversalTime(),
            EndTime = r.EndTime.ToUniversalTime(),
            AvailableKwh = r.AvailableKwh,
            PricePerKwh = r.PricePerKwh
        };

        await db.Slots.InsertOneAsync(slot);
        return slot;
    }

    public async Task<EnergyBookingSlot> UpdateAsync(string id, SlotRequest r)
    {
        var slot = await db.Slots
            .Find(x => x.Id == id)
            .FirstOrDefaultAsync() ?? throw new KeyNotFoundException("Slot not found.");

        if (r.EndTime <= r.StartTime)
            throw new InvalidOperationException("End time must be after start time.");

        slot.StartTime = r.StartTime.ToUniversalTime();
        slot.EndTime = r.EndTime.ToUniversalTime();
        slot.AvailableKwh = r.AvailableKwh;
        slot.PricePerKwh = r.PricePerKwh;
        slot.UpdatedAt = DateTime.UtcNow;

        await db.Slots.ReplaceOneAsync(x => x.Id == id, slot);
        return slot;
    }

    public async Task DeleteAsync(string id)
    {
        var hasActiveReservations = await db.Reservations
            .Find(x => x.SlotId == id && (x.Status == ReservationStatus.Pending || x.Status == ReservationStatus.Confirmed))
            .AnyAsync();

        if (hasActiveReservations)
            throw new InvalidOperationException("Slot has active reservations.");

        var deleteResult = await db.Slots.DeleteOneAsync(x => x.Id == id);
        if (deleteResult.DeletedCount == 0)
            throw new KeyNotFoundException("Slot not found.");
    }
}