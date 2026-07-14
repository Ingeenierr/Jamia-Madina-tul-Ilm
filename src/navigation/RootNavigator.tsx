import React from 'react';
import { NavigationContainer, DefaultTheme, DarkTheme } from '@react-navigation/native';
import { View, ActivityIndicator } from 'react-native';
import { GradientBackground } from '@/components/GradientBackground';
import { useTheme } from '@/theme';
import { useAuth } from '@/context/AuthContext';
import { AuthStack } from './AuthStack';
import { AdminTabs } from './AdminTabs';
import { TeacherTabs } from './TeacherTabs';

function SplashLoader() {
  const { colors } = useTheme();
  return (
    <GradientBackground variant="dusk">
      <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
        <ActivityIndicator color={colors.gold} size="large" />
      </View>
    </GradientBackground>
  );
}

export function RootNavigator() {
  const { user, initializing } = useAuth();
  const { colors, isDark } = useTheme();

  const navTheme = {
    ...(isDark ? DarkTheme : DefaultTheme),
    colors: {
      ...(isDark ? DarkTheme.colors : DefaultTheme.colors),
      background: colors.bgPaper,
      card: colors.bgPaper,
      border: colors.hairline,
      primary: colors.gold,
      text: colors.textPrimary,
    },
  };

  if (initializing) return <SplashLoader />;

  return (
    <NavigationContainer theme={navTheme}>
      {!user ? <AuthStack /> : user.role === 'ADMIN' ? <AdminTabs /> : <TeacherTabs />}
    </NavigationContainer>
  );
}
