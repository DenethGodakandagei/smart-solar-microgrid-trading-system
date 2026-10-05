using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolarMicrogrid.Api.Configuration;
using SmartSolarMicrogrid.Api.DTOs;
using SmartSolarMicrogrid.Api.Models;
using SmartSolarMicrogrid.Api.Services;

namespace SmartSolarMicrogrid.Api.Controllers;

[ApiController]
[Authorize(Roles = Roles.Prosumer)]
[Route("api/bookings")]
public sealed class BookingsController(IReservationService service) : ControllerBase
{
    [HttpPost]
    public async Task<ActionResult<EnergyReservation>> Create(BookingRequest request) =>
        Ok(await service.CreateBookingAsync(Nic(), request));

    [HttpGet]
    public async Task<ActionResult<List<EnergyReservation>>> Get([FromQuery] string? state) =>
        Ok(await service.QueryAsync(Nic(), state));

    private string Nic() =>
        User.FindFirstValue("nic") ?? throw new InvalidOperationException("Prosumer identity required.");
}