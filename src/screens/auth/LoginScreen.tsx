import React, { useState } from 'react';
import { StyleSheet, Text, View, KeyboardAvoidingView, Platform, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import Animated, { FadeInDown, FadeInUp } from 'react-native-reanimated';
import { GradientBackground } from '@/components/GradientBackground';
import { GlassCard } from '@/components/GlassCard';
import { AppTextInput } from '@/components/AppTextInput';
import { PrimaryButton } from '@/components/PrimaryButton';
import { type, useTheme } from '@/theme';
import { useAuth } from '@/context/AuthContext';

export function LoginScreen({ navigation }: any) {
  const { colors } = useTheme();
  const { login, error, clearError } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    clearError();
    setLoading(true);
    try {
      await login(email, password);
    } catch {
      // error surfaced via context
    } finally {
      setLoading(false);
    }
  };

  return (
    <GradientBackground variant="dusk">
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={{ flex: 1 }}>
        <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
          <Animated.View entering={FadeInDown.duration(600).springify()} style={styles.brandWrap}>
            <View style={[styles.crest, { backgroundColor: colors.goldSoft, borderColor: colors.hairline }]}>
              <Ionicons name="moon" size={30} color={colors.gold} />
            </View>
            <Text style={[type.displayXL, { color: colors.textOnDark, marginTop: 18 }]}>Jamia Madinat-ul-Ilm</Text>
            <Text style={[type.bodyM, { color: colors.textOnDarkMuted, marginTop: 4 }]}>
              Madrasa management, in one place
            </Text>
          </Animated.View>

          <Animated.View entering={FadeInUp.duration(600).delay(150).springify()}>
            <GlassCard tone="dark" glow style={{ marginTop: 32 }}>
              <Text style={[type.displayM, { color: colors.textOnDark, marginBottom: 18 }]}>Welcome back</Text>

              <AppTextInput
                label="Email"
                icon="mail-outline"
                tone="dark"
                autoCapitalize="none"
                keyboardType="email-address"
                value={email}
                onChangeText={setEmail}
                placeholder="you@jamia.edu"
              />
              <AppTextInput
                label="Password"
                icon="lock-closed-outline"
                tone="dark"
                secureTextEntry
                value={password}
                onChangeText={setPassword}
                placeholder="••••••••"
              />

              {!!error && (
                <Text style={[type.bodyS, { color: colors.statusRejected, marginBottom: 12 }]}>{error}</Text>
              )}

              <PrimaryButton label="Sign in" onPress={handleLogin} loading={loading} disabled={!email || !password} />

              <PrimaryButton
                label="Create an account"
                variant="ghost"
                onPress={() => navigation.navigate('SignUp')}
                style={{ marginTop: 4 }}
              />
            </GlassCard>
          </Animated.View>
        </ScrollView>
      </KeyboardAvoidingView>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  scroll: { flexGrow: 1, padding: 24, justifyContent: 'center' },
  brandWrap: { alignItems: 'center' },
  crest: {
    width: 72,
    height: 72,
    borderRadius: 22,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
