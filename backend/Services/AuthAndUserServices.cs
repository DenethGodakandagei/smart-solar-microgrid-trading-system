using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.AspNetCore.Identity;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;
using MongoDB.Driver;
using SmartSolarMicrogrid.Api.Configuration;
using SmartSolarMicrogrid.Api.Data;
using SmartSolarMicrogrid.Api.DTOs;
using SmartSolarMicrogrid.Api.Models;

namespace SmartSolarMicrogrid.Api.Services;

public interface IAuthService
{
    Task<LoginResponse> LoginAsync(LoginRequest request);
}

public sealed class AuthService(MongoContext db, IOptions<JwtOptions> jwt) : IAuthService
{
    public async Task<LoginResponse> LoginAsync(LoginRequest r)
    {
        // Authenticate strictly using NIC
        var nic = r.Nic ?? throw new InvalidOperationException("NIC is required for login.");

        var user = await db.Users
            .Find(x => x.ProsumerNic == nic || x.Username == nic)
            .FirstOrDefaultAsync() ?? throw new InvalidOperationException("Invalid NIC or password.");

        var hasher = new PasswordHasher<User>();
        var passwordVerification = hasher.VerifyHashedPassword(user, user.PasswordHash, r.Password);

        if (!user.IsActive || passwordVerification != PasswordVerificationResult.Success)
        {
            throw new InvalidOperationException("Invalid NIC or password.");
        }

        var options = jwt.Value;
        var expiration = DateTime.UtcNow.AddMinutes(options.ExpiryMinutes);

        var claims = new[]
        {
            new Claim(ClaimTypes.NameIdentifier, user.Id),
            new Claim(ClaimTypes.Name, user.Username),
            new Claim(ClaimTypes.Role, user.Role),
            new Claim("nic", user.ProsumerNic ?? "")
        };

        var tokenDescriptor = new JwtSecurityToken(
            options.Issuer,
            options.Audience,
            claims,
            expires: expiration,
            signingCredentials: new SigningCredentials(
                new SymmetricSecurityKey(Encoding.UTF8.GetBytes(options.Key)),
                SecurityAlgorithms.HmacSha256
            )
        );

        var token = new JwtSecurityTokenHandler().WriteToken(tokenDescriptor);

        return new LoginResponse(
            token,
            expiration,
            user.Username,
            user.Role,
            user.ProsumerNic
        );
    }
}

public interface IUserService
{
    Task<List<User>> GetAsync();
    Task<User> CreateAsync(UserRequest r);
    Task<User> UpdateAsync(string id, UserUpdateRequest r);
}

public sealed class UserService(MongoContext db) : IUserService
{
    public Task<List<User>> GetAsync() => db.Users.Find(_ => true).ToListAsync();

    public async Task<User> CreateAsync(UserRequest r)
    {
        var validRoles = new[] { Roles.Backoffice, Roles.GridOperator, Roles.Prosumer };
        if (!validRoles.Contains(r.Role))
            throw new InvalidOperationException("Invalid role.");

        if (await db.Users.Find(x => x.Username == r.Username).AnyAsync())
            throw new InvalidOperationException("Username already exists.");

        if (r.Role == Roles.Prosumer && string.IsNullOrWhiteSpace(r.ProsumerNic))
            throw new InvalidOperationException("Prosumer NIC is required.");

        var user = new User
        {
            Username = r.Username,
            Role = r.Role,
            ProsumerNic = r.ProsumerNic
        };

        user.PasswordHash = new PasswordHasher<User>().HashPassword(user, r.Password);

        await db.Users.InsertOneAsync(user);
        return user;
    }

    public async Task<User> UpdateAsync(string id, UserUpdateRequest r)
    {
        var user = await db.Users
            .Find(x => x.Id == id)
            .FirstOrDefaultAsync() ?? throw new KeyNotFoundException("User not found.");

        var validRoles = new[] { Roles.Backoffice, Roles.GridOperator, Roles.Prosumer };
        if (!validRoles.Contains(r.Role))
            throw new InvalidOperationException("Invalid role.");

        user.Role = r.Role;
        user.IsActive = r.IsActive;
        user.UpdatedAt = DateTime.UtcNow;

        await db.Users.ReplaceOneAsync(x => x.Id == id, user);
        return user;
    }
}

public interface IProsumerService
{
    Task<Prosumer> RegisterAsync(ProsumerRequest r);
    Task<Prosumer> GetAsync(string nic);
    Task<Prosumer> UpdateAsync(string nic, ProsumerUpdateRequest r);
    Task RequestDeactivationAsync(string nic);
}

public sealed class ProsumerService(MongoContext db) : IProsumerService
{
    public async Task<Prosumer> RegisterAsync(ProsumerRequest r)
    {
        if (await db.Prosumers.Find(x => x.Nic == r.Nic).AnyAsync())
            throw new InvalidOperationException("NIC already registered.");

        var prosumer = new Prosumer
        {
            Nic = r.Nic,
            FullName = r.FullName,
            Email = r.Email,
            Phone = r.Phone,
            Address = r.Address
        };

        var user = new User
        {
            Username = r.Nic,
            Role = Roles.Prosumer,
            ProsumerNic = r.Nic
        };

        user.PasswordHash = new PasswordHasher<User>().HashPassword(user, r.Password);

        await db.Prosumers.InsertOneAsync(prosumer);
        await db.Users.InsertOneAsync(user);

        return prosumer;
    }

    public async Task<Prosumer> GetAsync(string nic)
    {
        return await db.Prosumers
            .Find(x => x.Nic == nic)
            .FirstOrDefaultAsync() ?? throw new KeyNotFoundException("Prosumer not found.");
    }

    public async Task<Prosumer> UpdateAsync(string nic, ProsumerUpdateRequest r)
    {
        var prosumer = await GetAsync(nic);

        prosumer.FullName = r.FullName;
        prosumer.Email = r.Email;
        prosumer.Phone = r.Phone;
        prosumer.Address = r.Address;
        prosumer.UpdatedAt = DateTime.UtcNow;

        await db.Prosumers.ReplaceOneAsync(x => x.Nic == nic, prosumer);
        return prosumer;
    }

    public async Task RequestDeactivationAsync(string nic)
    {
        var prosumer = await GetAsync(nic);

        prosumer.DeactivationRequested = true;
        prosumer.UpdatedAt = DateTime.UtcNow;

        await db.Prosumers.ReplaceOneAsync(x => x.Nic == nic, prosumer);
    }
}