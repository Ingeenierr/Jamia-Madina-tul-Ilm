import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { FloatingTabBar } from '@/components/FloatingTabBar';
import { TeacherDashboardScreen } from '@/screens/teacher/TeacherDashboardScreen';
import { LeaveRequestScreen } from '@/screens/teacher/LeaveRequestScreen';
import { AttendanceScreen } from '@/screens/attendance/AttendanceScreen';
import { ClassesScreen } from '@/screens/classes/ClassesScreen';
import { MoreScreen } from '@/screens/more/MoreScreen';
import { ComingSoonScreen } from '@/screens/shared/ComingSoonScreen';

const Tab = createBottomTabNavigator();
const Stack = createNativeStackNavigator();

function HomeStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="TeacherHome" component={TeacherDashboardScreen} />
      <Stack.Screen name="Attendance" component={AttendanceScreen} />
      <Stack.Screen name="LeaveRequest" component={LeaveRequestScreen} />
    </Stack.Navigator>
  );
}

function MoreStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="MoreHome" component={MoreScreen} />
      <Stack.Screen name="Search" component={ComingSoonScreen} initialParams={{ title: 'Search records', icon: 'search-outline' }} />
    </Stack.Navigator>
  );
}

export function TeacherTabs() {
  return (
    <Tab.Navigator tabBar={(props) => <FloatingTabBar {...props} />} screenOptions={{ headerShown: false }}>
      <Tab.Screen name="Home" component={HomeStack} />
      <Tab.Screen name="Classes" component={ClassesScreen} />
      <Tab.Screen name="Attendance" component={AttendanceScreen} />
      <Tab.Screen name="More" component={MoreStack} />
    </Tab.Navigator>
  );
}
