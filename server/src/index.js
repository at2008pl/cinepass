import express from 'express';
import dotenv from 'dotenv';
import cors from 'cors';
import path from 'path';
import { fileURLToPath } from 'url';
import authRoutes from './routes/auth.js';
import feedRoutes from './routes/feed.js';
import usersRoutes from './routes/users.js';
import eventsRoutes from './routes/events.js';
import ticketsRoutes from './routes/tickets.js';
import downloadRoutes from './routes/download.js';
import referralRoutes from './routes/referral.js';
import { checkDbConnection } from './db.js';
import { initializeDatabase } from './bootstrap.js';

dotenv.config();

const app = express();
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

app.use(cors());
app.use(express.json());
app.use('/media', express.static(path.join(__dirname, '../media')));

app.use('/api/auth', authRoutes);
app.use('/api/feed', feedRoutes);
app.use('/api/users', usersRoutes);
app.use('/api/events', eventsRoutes);
app.use('/api/tickets', ticketsRoutes);
app.use('/', referralRoutes);
app.use('/download', downloadRoutes);

const PORT = process.env.PORT || 4000;
app.listen(PORT, async () => {
  console.log(`Server running on port ${PORT}`);
  await checkDbConnection();
  await initializeDatabase();
});
