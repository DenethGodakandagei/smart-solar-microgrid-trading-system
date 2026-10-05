using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using SmartSolarMicrogrid.Api.Configuration;
using SmartSolarMicrogrid.Api.Data;
using SmartSolarMicrogrid.Api.Middleware;
using SmartSolarMicrogrid.Api.Services;

var builder = WebApplication.CreateBuilder(args);

// ---------------------------------------------------------------
// Configuration
// ---------------------------------------------------------------
builder.Services.Configure<MongoDbOptions>(builder.Configuration.GetSection("MongoDb"));
builder.Services.Configure<JwtOptions>(builder.Configuration.GetSection("Jwt"));

// Read JWT settings safely. Fail at startup with a clear message
// instead of a NullReferenceException on the first request.
var jwtSection = builder.Configuration.GetSection("Jwt");
if (!jwtSection.Exists())
{
    throw new InvalidOperationException(
        "The 'Jwt' section is missing. Make sure appsettings.json is in the project folder " +
        "(next to Program.cs) and contains a \"Jwt\" section with Key, Issuer and Audience.");
}

var jwtKey = jwtSection["Key"];
var jwtIssuer = jwtSection["Issuer"];
var jwtAudience = jwtSection["Audience"];

if (string.IsNullOrWhiteSpace(jwtKey) || jwtKey.Length < 32)
{
    throw new InvalidOperationException("Jwt:Key must be set and at least 32 characters long.");
}

// ---------------------------------------------------------------
// Authentication / Authorization
// ---------------------------------------------------------------
builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(o =>
    {
        o.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            ValidIssuer = jwtIssuer,
            ValidAudience = jwtAudience,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtKey)),
            ClockSkew = TimeSpan.Zero
        };
    });

builder.Services.AddAuthorization();
builder.Services.AddControllers();

builder.Services.AddCors(o => o.AddPolicy("Clients", p =>
    p.AllowAnyOrigin().AllowAnyHeader().AllowAnyMethod()));

// ---------------------------------------------------------------
// Swagger
// ---------------------------------------------------------------
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(o =>
{
    o.SwaggerDoc("v1", new OpenApiInfo { Title = "Smart Solar Microgrid API", Version = "v1" });

    o.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Name = "Authorization",
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        In = ParameterLocation.Header
    });

    o.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            Array.Empty<string>()
        }
    });
});

// ---------------------------------------------------------------
// Application services
// ---------------------------------------------------------------
builder.Services.AddSingleton<MongoContext>();
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IUserService, UserService>();
builder.Services.AddScoped<IProsumerService, ProsumerService>();
builder.Services.AddScoped<IStationService, StationService>();
builder.Services.AddScoped<ISlotService, SlotService>();
builder.Services.AddScoped<IReservationService, ReservationService>();
builder.Services.AddSingleton<DatabaseSeeder>();

// ---------------------------------------------------------------
// Pipeline
// ---------------------------------------------------------------
var app = builder.Build();

app.UseMiddleware<ExceptionHandlingMiddleware>();
app.UseSwagger();
app.UseSwaggerUI();
app.UseCors("Clients");
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

using (var scope = app.Services.CreateScope())
{
    try
    {
        await scope.ServiceProvider.GetRequiredService<DatabaseSeeder>().SeedAsync();
    }
    catch (Exception ex)
    {
        app.Logger.LogError(ex, "Database seeding failed. The API started, but MongoDB is unavailable.");
    }
}

app.Run();