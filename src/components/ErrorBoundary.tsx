import React from 'react';
import { StyleSheet, Text, View, Pressable } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';
import { lightColors } from '@/theme/palettes';
import { type } from '@/theme/typography';

interface State {
  hasError: boolean;
  message?: string;
}

/**
 * Last line of defense: if any screen throws during render (a bad Firebase
 * payload, an unexpected null, whatever), this catches it and shows a
 * recoverable screen instead of a blank white crash.
 *
 * Deliberately built with zero dependency on ThemeContext, AuthContext, or
 * any other app-level provider — those could themselves be the thing that
 * broke, and a fallback screen that depends on the same context tree it's
 * meant to protect against isn't a real fallback.
 */
export class ErrorBoundary extends React.Component<{ children: React.ReactNode }, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, message: error.message };
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    console.error('ErrorBoundary caught:', error, info.componentStack);
  }

  reset = () => this.setState({ hasError: false, message: undefined });

  render() {
    if (!this.state.hasError) return this.props.children;

    return (
      <View style={styles.fill}>
        <LinearGradient colors={lightColors.duskGradient as unknown as string[]} style={StyleSheet.absoluteFill} />
        <View style={styles.wrap}>
          <View style={styles.iconWrap}>
            <Ionicons name="alert-circle-outline" size={30} color={lightColors.statusRejected} />
          </View>
          <Text style={[type.displayL, { color: lightColors.textOnDark, marginTop: 18, textAlign: 'center' }]}>
            Something went wrong
          </Text>
          <Text style={[type.bodyM, { color: lightColors.textOnDarkMuted, marginTop: 8, textAlign: 'center' }]}>
            The screen hit an unexpected error. Your data is safe — try again.
          </Text>
          <Pressable onPress={this.reset} style={styles.button}>
            <Text style={[type.button, { color: lightColors.inkGreen }]}>Try again</Text>
          </Pressable>
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  fill: { flex: 1 },
  wrap: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 32 },
  iconWrap: {
    width: 64,
    height: 64,
    borderRadius: 20,
    backgroundColor: 'rgba(194,74,63,0.16)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  button: {
    marginTop: 24,
    minWidth: 160,
    height: 52,
    borderRadius: 16,
    backgroundColor: lightColors.gold,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
});
