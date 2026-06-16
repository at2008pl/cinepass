import express from 'express';
import { listFeedPosts } from '../controllers/feedController.js';

const router = express.Router();

router.get('/posts', listFeedPosts);

export default router;
