using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Bson;
using MongoDB.Driver;
using SmartSolarMicrogrid.Api.Data;

namespace SmartSolarMicrogrid.Api.Controllers;

[ApiController]
[AllowAnonymous]
[Route("api/health")]
public sealed class DatabaseHealthController(MongoContext db, ILogger<DatabaseHealthController> logger)
    : ControllerBase
{
    [HttpGet("database")]
    public async Task<IActionResult> Database()
    {
        try
        {
            await db.Database.RunCommandAsync<BsonDocument>(new BsonDocument("ping", 1));
            return Ok(new
            {
                status = "connected",
                database = db.Database.DatabaseNamespace.DatabaseName
            });
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "MongoDB health check failed.");
            return StatusCode(StatusCodes.Status503ServiceUnavailable, new
            {
                status = "disconnected",
                error = ex.GetType().Name,
                message = ex.Message
            });
        }
    }
}