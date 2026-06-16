import express from 'express';
import {
  getLeaderboard,
  getProfile,
  getReferralStats,
  getReferralTree,
  getWallet,
  getWalletTransactions,
  redeemCoins,
} from '../controllers/usersController.js';

const router = express.Router();

router.get('/profile', getProfile);
router.get('/wallet', getWallet);
router.get('/wallet/transactions', getWalletTransactions);
router.post('/wallet/redeem', redeemCoins);
router.get('/leaderboard', getLeaderboard);
router.get('/referrals', getReferralStats);
router.get('/referrals/tree', getReferralTree);

export default router;

