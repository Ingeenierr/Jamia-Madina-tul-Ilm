import { getApp, getApps, initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getDatabase } from 'firebase/database';

const firebaseConfig = {
  apiKey: 'AIzaSyCZvRZP-lhvaJwLYGy0rs3ovkSDhmlS9sM',
  authDomain: 'jamia-madina-tul-ilmm.firebaseapp.com',
  databaseURL: 'https://jamia-madina-tul-ilmm-default-rtdb.asia-southeast1.firebasedatabase.app',
  projectId: 'jamia-madina-tul-ilmm',
  storageBucket: 'jamia-madina-tul-ilmm.firebasestorage.app',
  messagingSenderId: '1095504790264',
  appId: '1:1095504790264:web:1a629e230ee66cace4f10d',
  measurementId: 'G-9BVEGVH66X',
};

const app = getApps().length ? getApp() : initializeApp(firebaseConfig);

export const auth = (() => {
  return getAuth(app);
})();

export const db = getDatabase(app);

export default app;
