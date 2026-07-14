import React, { createContext, useCallback, useContext, useRef, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withSpring,
  withTiming,
  runOnJS,
  SharedValue,
} from 'react-native-reanimated';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import * as Haptics from 'expo-haptics';
import { radius, type, useTheme } from '@/theme';

type ToastKind = 'success' | 'error' | 'info';

interface ToastState {
  id: number;
  message: string;
  kind: ToastKind;
}

interface ToastContextValue {
  show: (message: string, kind?: ToastKind) => void;
}

const ToastContext = createContext<ToastContextValue | undefined>(undefined);

const ICONS: Record<ToastKind, keyof typeof Ionicons.glyphMap> = {
  success: 'checkmark-circle',
  error: 'alert-circle',
  info: 'information-circle',
};

/**
 * A single toast at a time, queued — replaces jarring Alert.alert() for
 * routine success/failure feedback (saved, deleted, network hiccup) so the
 * app never interrupts the person with a modal for something that isn't a
 * decision they need to make.
 */
export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toast, setToast] = useState<ToastState | null>(null);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const translateY = useSharedValue(-120);
  const opacity = useSharedValue(0);

  const dismiss = useCallback(() => {
    translateY.value = withTiming(-120, { duration: 220 });
    opacity.value = withTiming(0, { duration: 200 }, () => runOnJS(setToast)(null));
  }, []);

  const show = useCallback((message: string, kind: ToastKind = 'info') => {
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    setToast({ id: Date.now(), message, kind });
    if (kind === 'error') Haptics.notificationAsync(Haptics.NotificationFeedbackType.Error).catch(() => {});
    else if (kind === 'success') Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success).catch(() => {});
    translateY.value = withSpring(0, { damping: 16, stiffness: 220 });
    opacity.value = withTiming(1, { duration: 180 });
    timeoutRef.current = setTimeout(dismiss, 2600);
  }, [dismiss]);

  return (
    <ToastContext.Provider value={{ show }}>
      {children}
      {toast && <ToastBanner toast={toast} translateY={translateY} opacity={opacity} />}
    </ToastContext.Provider>
  );
}

function ToastBanner({
  toast,
  translateY,
  opacity,
}: {
  toast: ToastState;
  translateY: SharedValue<number>;
  opacity: SharedValue<number>;
}) {
  const { colors } = useTheme();
  const insets = useSafeAreaInsets();
  const animatedStyle = useAnimatedStyle(() => ({
    transform: [{ translateY: translateY.value }],
    opacity: opacity.value,
  }));

  const tint =
    toast.kind === 'success' ? colors.statusApproved : toast.kind === 'error' ? colors.statusRejected : colors.gold;

  return (
    <Animated.View
      pointerEvents="none"
      style={[styles.wrap, { top: insets.top + 8 }, animatedStyle]}
    >
      <View style={[styles.banner, { backgroundColor: colors.glassDark, borderColor: tint }]}>
        <Ionicons name={ICONS[toast.kind]} size={18} color={tint} />
        <Text style={[type.bodyM, { color: colors.textOnDark, marginLeft: 10, flex: 1 }]} numberOfLines={2}>
          {toast.message}
        </Text>
      </View>
    </Animated.View>
  );
}

export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
}

const styles = StyleSheet.create({
  wrap: { position: 'absolute', left: 16, right: 16, zIndex: 999 },
  banner: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: radius.md,
    borderWidth: 1,
    paddingVertical: 12,
    paddingHorizontal: 14,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.25,
    shadowRadius: 12,
    elevation: 8,
  },
});
