using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace SmartSolarMicrogrid.Api.DTOs;

/// <summary>
/// Login body. The Android app sends nic or email; Swagger can still send username.
/// </summary>
public sealed class LoginRequest
{
    [JsonPropertyName("nic")] public string? Nic { get; set; }
    [JsonPropertyName("email")] public string? Email { get; set; }
    [JsonPropertyName("username")] public string? Username { get; set; }
    [JsonPropertyName("password")] public string Password { get; set; } = string.Empty;
}

/// <summary>
/// Login response. token/nic/fullName/email/role/status are what the Android app reads.
/// expiresAt, username and prosumerNic are kept so existing AuthService code still compiles.
/// </summary>
public sealed record LoginResponse
{
    [JsonPropertyName("token")] public string Token { get; set; } = string.Empty;
    [JsonPropertyName("nic")] public string Nic { get; set; } = string.Empty;
    [JsonPropertyName("fullName")] public string? FullName { get; set; }
    [JsonPropertyName("email")] public string? Email { get; set; }
    [JsonPropertyName("role")] public string Role { get; set; } = string.Empty;
    [JsonPropertyName("status")] public string Status { get; set; } = "Active";

    [JsonPropertyName("expiresAt")] public DateTime ExpiresAt { get; set; }
    [JsonPropertyName("username")] public string? Username { get; set; }
    [JsonPropertyName("prosumerNic")] public string? ProsumerNic { get; set; }

    public LoginResponse()
    {
    }

    /// <summary>
    /// Keeps the existing AuthService call working:
    /// new LoginResponse(token, expiresAt, username, role, prosumerNic)
    /// </summary>
    public LoginResponse(string token, DateTime expiresAt, string? username, string role, string? prosumerNic)
    {
        Token = token;
        ExpiresAt = expiresAt;
        Username = username;
        Role = role;
        ProsumerNic = prosumerNic;
        Nic = !string.IsNullOrWhiteSpace(prosumerNic) ? prosumerNic : username ?? string.Empty;
        FullName = username;
    }
}

public sealed class UserRequest
{
    [Required, MinLength(3)] public string Username { get; set; } = "";
    [Required, MinLength(6)] public string Password { get; set; } = "";
    [Required] public string Role { get; set; } = "";
    public string? ProsumerNic { get; set; }
}

public sealed class UserUpdateRequest
{
    [Required] public string Role { get; set; } = "";
    public bool IsActive { get; set; } = true;
}

public sealed class ProsumerRequest
{
    [Required] public string Nic { get; set; } = "";
    [Required] public string FullName { get; set; } = "";
    [Required, EmailAddress] public string Email { get; set; } = "";
    [Required] public string Phone { get; set; } = "";
    public string Address { get; set; } = "";
    [Required, MinLength(6)] public string Password { get; set; } = "";
}

public sealed class ProsumerUpdateRequest
{
    [Required] public string FullName { get; set; } = "";
    [Required, EmailAddress] public string Email { get; set; } = "";
    [Required] public string Phone { get; set; } = "";
    public string Address { get; set; } = "";
}

public sealed class StationRequest
{
    [Required] public string Name { get; set; } = "";
    [Required] public string Location { get; set; } = "";
    [Range(-90, 90)] public double Latitude { get; set; }
    [Range(-180, 180)] public double Longitude { get; set; }
    [Range(0.01, double.MaxValue)] public decimal CapacityKwh { get; set; }
    [Range(1, int.MaxValue)] public int BatteryStorageSlots { get; set; }
}

public sealed class SlotRequest
{
    [Required] public string StationId { get; set; } = "";
    public DateTime StartTime { get; set; }
    public DateTime EndTime { get; set; }
    [Range(0.01, double.MaxValue)] public decimal AvailableKwh { get; set; }
    [Range(0, double.MaxValue)] public decimal PricePerKwh { get; set; }
}

public sealed class ReservationRequest
{
    [Required] public string SlotId { get; set; } = "";
    [Range(0.01, double.MaxValue)] public decimal EnergyKwh { get; set; }
}

public sealed class ReservationUpdateRequest
{
    [Range(0.01, double.MaxValue)] public decimal EnergyKwh { get; set; }
}

public sealed class QrVerifyRequest
{
    [Required] public string QrToken { get; set; } = "";
}

public sealed record DashboardResponse(
    int ActiveStations,
    int AvailableSlots,
    int PendingReservations,
    int CompletedReservations,
    decimal AvailableEnergyKwh);