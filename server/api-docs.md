# CinePass Server API Documentation

## POST /api/auth/register
Register a new user.

### Request Body (JSON)
| Field         | Type   | Required | Description                       |
|---------------|--------|----------|-----------------------------------|
| name          | string | Yes      | Full name                         |
| phone         | string | Yes      | Phone number                      |
| email         | string | Yes      | Email address                     |
| gender        | string | No       | Gender (male/female/other)        |
| dob           | string | No       | Date of birth (YYYY-MM-DD)        |
| referral_code | string | No       | Referral code                     |
| address_line  | string | No       | Address (one line)                |
| city          | string | No       | City                              |
| state         | string | No       | State                             |
| pincode       | string | No       | Pincode                           |
| password      | string | Yes      | Password                          |
| confirmPassword | string | Yes    | Confirm password                  |
| otp           | string | Yes      | 4-digit OTP (simulated, any 4 digits)

### Response
- 201: `{ success: true, user: { ... } }`
- 400: `{ success: false, message: '...' }`
- 409: `{ success: false, message: 'Email already exists' }`

## POST /api/auth/login
Login with email and password.

### Request Body (JSON)
| Field    | Type   | Required | Description      |
|----------|--------|----------|------------------|
| email    | string | Yes      | Email address    |
| password | string | Yes      | Password         |

### Response
- 200: `{ success: true, user: { ... } }`
- 400/401: `{ success: false, message: '...' }`

## GET /api/feed/posts
Fetch home feed posts for image/video content.

### Query params
| Param   | Type   | Required | Description |
|---------|--------|----------|-------------|
| section | string | No       | One of `hero`, `trending`, `updates`, `trailers` |

### Response
- 200:
```json
{
  "success": true,
  "message": "Feed loaded",
  "data": {
    "posts": [
      {
        "id": 1,
        "title": "Pyaar",
        "section": "hero",
        "media_type": "image",
        "media_url": "/media/images/pyaar-hero.jpg"
      }
    ]
  }
}
```
