import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { FloatingTabBar } from '@/components/FloatingTabBar';
import { AdminDashboardScreen } from '@/screens/admin/AdminDashboardScreen';
import { StudentsListScreen } from '@/screens/students/StudentsListScreen';
import { StudentProfileScreen } from '@/screens/students/StudentProfileScreen';
import { TeachersListScreen } from '@/screens/teachers/TeachersListScreen';
import { ApprovalScreen } from '@/screens/admin/ApprovalScreen';
import { LeaveRequestsAdminScreen } from '@/screens/admin/LeaveRequestsAdminScreen';
import { ClassesScreen } from '@/screens/classes/ClassesScreen';
import { AttendanceScreen } from '@/screens/attendance/AttendanceScreen';
import { AttendanceHistoryScreen } from '@/screens/attendance/AttendanceHistoryScreen';
import { MoreScreen } from '@/screens/more/MoreScreen';
import { FinanceHomeScreen } from '@/screens/finance/FinanceHomeScreen';
import { DonationsScreen } from '@/screens/finance/DonationsScreen';
import { PayrollScreen } from '@/screens/finance/PayrollScreen';
import { FeesScreen } from '@/screens/finance/FeesScreen';
import { CanteenScreen } from '@/screens/finance/CanteenScreen';
import { LibraryScreen } from '@/screens/library/LibraryScreen';
import { BookDetailScreen } from '@/screens/library/BookDetailScreen';
import { ShopScreen } from '@/screens/shop/ShopScreen';
import { ShopSalesHistoryScreen } from '@/screens/shop/ShopSalesHistoryScreen';
import { ComingSoonScreen } from '@/screens/shared/ComingSoonScreen';

const Tab = createBottomTabNavigator();
const Stack = createNativeStackNavigator();

// Wrap list screens with their detail screens so e.g. tapping a student
// pushes StudentProfile within the "Students" tab, keeping the tab bar visible.
function HomeStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="AdminHome" component={AdminDashboardScreen} />
      <Stack.Screen name="Attendance" component={AttendanceScreen} />
      <Stack.Screen name="AttendanceHistory" component={AttendanceHistoryScreen} />
      <Stack.Screen name="Classes" component={ClassesScreen} />
      <Stack.Screen name="Finance" component={FinanceHomeScreen} />
      <Stack.Screen name="Donations" component={DonationsScreen} />
      <Stack.Screen name="Payroll" component={PayrollScreen} />
      <Stack.Screen name="Fees" component={FeesScreen} />
      <Stack.Screen name="Canteen" component={CanteenScreen} />
    </Stack.Navigator>
  );
}

function StudentsStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="StudentsList" component={StudentsListScreen} />
      <Stack.Screen name="StudentProfile" component={StudentProfileScreen} />
    </Stack.Navigator>
  );
}

function MoreStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="MoreHome" component={MoreScreen} />
      <Stack.Screen name="Library" component={LibraryScreen} />
      <Stack.Screen name="BookDetail" component={BookDetailScreen} />
      <Stack.Screen name="Shop" component={ShopScreen} />
      <Stack.Screen name="ShopSales" component={ShopSalesHistoryScreen} />
      <Stack.Screen name="Search" component={ComingSoonScreen} initialParams={{ title: 'Search records', icon: 'search-outline' }} />
    </Stack.Navigator>
  );
}

export function AdminTabs() {
  return (
    <Tab.Navigator tabBar={(props) => <FloatingTabBar {...props} />} screenOptions={{ headerShown: false }}>
      <Tab.Screen name="Home" component={HomeStack} />
      <Tab.Screen name="Students" component={StudentsStack} />
      <Tab.Screen name="Teachers" component={TeachersListScreen} />
      <Tab.Screen name="Approvals" component={ApprovalScreen} />
      <Tab.Screen name="Leaves" component={LeaveRequestsAdminScreen} />
      <Tab.Screen name="More" component={MoreStack} />
    </Tab.Navigator>
  );
}
