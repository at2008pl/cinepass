import express from 'express';
import { bookTicket, cancelTicket, getMyTickets, getTicketById } from '../controllers/ticketsController.js';

const router = express.Router();

// router.post('/book', bookTicket); // Disabled temporarily
router.get('/mine', getMyTickets);
router.get('/:ticketId', getTicketById);
router.delete('/:ticketId', cancelTicket);

export default router;

