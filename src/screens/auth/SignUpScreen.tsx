import React, { useState } from 'react';
import { StyleSheet, Text, View, Pressable, ScrollView, KeyboardAvoidingView, Platform } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import Animated, { FadeInUp } from 'react-native-reanimated';
import { GradientBackground } from '@/components/GradientBackground';
import { GlassCard } from '@/components/GlassCard';
import { AppTextInput } from '@/components/AppTextInput';
import { PrimaryButton } from '@/components/PrimaryButton';
import { radius, type, useTheme } from '@/theme';
import { useAuth } from '@/context/AuthContext';
import { UserRole } from '@/types/models';

export function SignUpScreen({ navigation }: any) {
  const { colors } = useTheme();
  const { signUp, error, clearError } = useAuth();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('TEACHER');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  const handleSubmit = async () => {
    clearError();
    setLoading(true);
    try {
      await signUp({ name, email, password, role });
      setDone(true);
    } catch {
      // surfaced via context
    } finally {
      setLoading(false);
    }
  };

  return (
    <GradientBackground variant="dusk">
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={{ flex: 1 }}>
        <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
          <Pressable onPress={() => navigation.goBack()} style={styles.back} hitSlop={12}>
            <Ionicons name="arrow-back" size={20} color={colors.textOnDark} />
          </Pressable>

          <Text style={[type.displayXL, { color: colors.textOnDark }]}>Join the ledger</Text>
          <Text style={[type.bodyM, { color: colors.textOnDarkMuted, marginTop: 4, marginBottom: 24 }]}>
            Teacher accounts need admin approval before first sign-in.
          </Text>

          {done ? (
            <Animated.View entering={FadeInUp.duration(500)}>
              <GlassCard tone="dark" glow>
                <Ionicons name="checkmark-circle" size={40} color={colors.statusApproved} />
                <Text style={[type.displayM, { color: colors.textOnDark, marginTop: 12 }]}>Account created</Text>
                <Text style={[type.bodyM, { color: colors.textOnDarkMuted, marginTop: 6, marginBottom: 18 }]}>
                  {role === 'TEACHER'
                    ? "You'll be able to sign in once an admin approves your account."
                    : 'You can sign in now.'}
                </Text>
                <PrimaryButton label="Back to sign in" onPress={() => navigation.navigate('Login')} />
              </GlassCard>
            </Animated.View>
          ) : (
            <GlassCard tone="dark" glow>
              <View style={styles.roleRow}>
                {(['TEACHER', 'ADMIN'] as UserRole[]).map((r) => (
                  <Pressable
                    key={r}
                    onPress={() => setRole(r)}
                    style={[
                      styles.roleChip,
                      { borderColor: colors.hairlineDark },
                      role === r && { backgroundColor: colors.gold, borderColor: colors.gold },
                    ]}
                  >
                    <Text style={[type.bodyM, { color: role === r ? colors.inkGreen : colors.textOnDark }]}>
                      {r === 'TEACHER' ? 'Teacher' : 'Admin'}
                    </Text>
                  </Pressable>
                ))}
              </View>

              <AppTextInput label="Full name" icon="person-outline" tone="dark" value={name} onChangeText={setName} placeholder="Your name" />
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
                placeholder="At least 6 characters"
              />

              {!!error && <Text style={[type.bodyS, { color: colors.statusRejected, marginBottom: 12 }]}>{error}</Text>}

              <PrimaryButton
                label="Create account"
                onPress={handleSubmit}
                loading={loading}
                disabled={!name || !email || password.length < 6}
              />
            </GlassCard>
          )}
        </ScrollView>
      </KeyboardAvoidingView>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  scroll: { flexGrow: 1, padding: 24, paddingTop: 60 },
  back: {
    width: 36,
    height: 36,
    borderRadius: 12,
    backgroundColor: 'rgba(255,255,255,0.08)',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  roleRow: { flexDirection: 'row', gap: 10, marginBottom: 20 },
  roleChip: {
    flex: 1,
    height: 44,
    borderRadius: radius.sm,
    borderWidth: 1.5,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
