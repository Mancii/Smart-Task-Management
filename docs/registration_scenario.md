# User Registration and Email Verification Flow

## Scenario: New User Registration

1. **User Submits Registration Form**
   - User fills out the registration form with:
     - Username
     - Email address
     - Password
     - Other required fields
   - Clicks "Register" button

2. **Server Processes Registration**
   - System validates the input data
   - Checks if email is already in use
   - Creates a new user account with `enabled = false`
   - Generates a unique verification token (valid for 24 hours)
   - Sends verification email to the provided email address
   - Returns success response with a message to check email

3. **User Receives Verification Email**
   - Email contains:
     - Greeting with username
     - Verification link with token
     - Expiration notice (24 hours)
     - Instructions to click the link

4. **User Clicks Verification Link**
   - System verifies the token:
     - Checks if token exists
     - Verifies token is not expired
     - Ensures token hasn't been used
   - If valid:
     - Sets user's `enabled` status to `true`
     - Marks token as used
     - Redirects to login page with success message
   - If invalid/expired:
     - Shows appropriate error message
     - Option to resend verification email

5. **User Logs In**
   - User attempts to log in with credentials
   - System verifies:
     - Email exists
     - Password is correct
     - Account is enabled (email verified)
   - If all checks pass:
     - Generates JWT tokens
     - Logs user in
   - If email not verified:
     - Returns error: "Please verify your email address first"
     - Option to resend verification email

## Error Handling

- **Email Already Exists**
  - Error message: "Email already in use"
  - Suggests password reset if needed

- **Expired Token**
  - Message: "Verification link has expired"
  - Option to resend verification email

- **Invalid Token**
  - Message: "Invalid verification link"
  - Suggests trying again or registering

- **Email Sending Failure**
  - Logs error server-side
  - Returns user-friendly message
  - Suggests trying again later

## Security Considerations

- Tokens are single-use
- 24-hour expiration on verification links
- Rate limiting on verification attempts
- Secure token generation using UUID
- No sensitive data in URL parameters
- HTTPS required for all endpoints

## Future Enhancements

- Add resend verification email functionality
- Implement account locking after failed attempts
- Add password strength requirements
- Include welcome email after successful verification
- Add logging for security events
