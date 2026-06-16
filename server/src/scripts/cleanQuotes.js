import pool from '../db.js';

async function run() {
  try {
    console.log('Cleaning quoted fields in users table...');
    const res = await pool.query(`
      UPDATE users
      SET
        name = regexp_replace(name, '^"(.*)"$', '\\1'),
        email = regexp_replace(email, '^"(.*)"$', '\\1'),
        phone = regexp_replace(phone, '^"(.*)"$', '\\1'),
        password = regexp_replace(password, '^"(.*)"$', '\\1'),
        referral_code = regexp_replace(referral_code, '^"(.*)"$', '\\1'),
        referred_by_code = regexp_replace(referred_by_code, '^"(.*)"$', '\\1'),
        address_line = regexp_replace(address_line, '^"(.*)"$', '\\1'),
        city = regexp_replace(city, '^"(.*)"$', '\\1'),
        state = regexp_replace(state, '^"(.*)"$', '\\1'),
        pincode = regexp_replace(pincode, '^"(.*)"$', '\\1')
      WHERE
        name LIKE '"%' OR email LIKE '"%' OR phone LIKE '"%' OR password LIKE '"%' OR referral_code LIKE '"%' OR referred_by_code LIKE '"%' OR address_line LIKE '"%' OR city LIKE '"%' OR state LIKE '"%' OR pincode LIKE '"%'
    `);

    console.log('Rows updated:', res.rowCount);
    await pool.end();
    console.log('Done.');
    process.exit(0);
  } catch (err) {
    console.error('Error cleaning quotes:', err);
    process.exit(1);
  }
}

run();
