import express from 'express';
import { getEvent, listEvents, registerForEvent } from '../controllers/eventsController.js';

const router = express.Router();

router.get('/', listEvents);
router.get('/:id', getEvent);
router.post('/:eventId/register', registerForEvent);

export default router;

