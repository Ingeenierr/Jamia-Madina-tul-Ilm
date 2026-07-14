import React from 'react';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { EmptyState } from '@/components/EmptyState';
import { Ionicons } from '@expo/vector-icons';

/**
 * Placeholder for modules not yet ported from the original Kotlin app
 * (Finance suite, Library, Shop, Search, etc). Follows the exact same
 * ScreenHeader + GlassCard pattern as the built screens, so wiring in the
 * real thing later is a drop-in replacement — see README "Extending".
 */
export function ComingSoonScreen({ route }: any) {
  const title: string = route?.params?.title ?? route?.name ?? 'Coming soon';
  const icon: keyof typeof Ionicons.glyphMap = route?.params?.icon ?? 'construct-outline';
  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow="Next up" title={title} />
      <EmptyState icon={icon} title={`${title} is next on the list`} subtitle="Ported using the same components and Firebase pattern as the rest of the app." />
    </GradientBackground>
  );
}
