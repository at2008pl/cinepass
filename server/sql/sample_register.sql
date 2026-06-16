-- Sample registration (password should be hashed in real app)
INSERT INTO users (name, phone, email, gender, dob, referral_code, address_line, city, state, pincode, password)
VALUES ('Test User', '9876543210', 'test@example.com', 'male', '2000-01-01', 'REF123', '123 Main St', 'Bengaluru', 'Karnataka', '560032', '$2b$10$hash');
