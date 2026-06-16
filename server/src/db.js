import pkg from 'pg';
import dotenv from 'dotenv';
dotenv.config();

const { Pool } = pkg;

const pool = new Pool({
  host: process.env.DB_HOST || '192.168.1.121',
  port: Number(process.env.DB_PORT || 5432),
  user: process.env.DB_USER || 'postgres',
  password: process.env.DB_PASSWORD || 'Animisha@2025',
  database: process.env.DB_NAME || 'Cineapp',
});

export async function checkDbConnection() {
  try {
    await pool.query('SELECT 1');
    console.log('[DB] Connected successfully');
  } catch (err) {
    console.error('[DB] Connection failed:', err.message);
  }
}

export default pool;
