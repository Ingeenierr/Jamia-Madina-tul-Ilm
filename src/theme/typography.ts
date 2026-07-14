/**
 * Type system: a restrained serif for the "ledger" headings (Lora), a clean
 * grotesk for interface text (Inter), and a monospace for anything that is
 * actually a number a admin needs to trust — fees, counts, dates
 * (IBM Plex Mono). Mixing three families only works because each one only
 * ever appears in its own lane.
 */
// Family names as exported by the @expo-google-fonts/* packages loaded via
// useFonts() in App.tsx — these strings must match exactly.
export const fontFamily = {
  display: 'Lora_600SemiBold',
  displayBold: 'Lora_700Bold',
  body: 'Inter_400Regular',
  bodyMedium: 'Inter_500Medium',
  bodySemiBold: 'Inter_600SemiBold',
  mono: 'IBMPlexMono_500Medium',
};

export const type = {
  displayXL: { fontFamily: fontFamily.displayBold, fontSize: 30, lineHeight: 36, letterSpacing: -0.3 },
  displayL: { fontFamily: fontFamily.display, fontSize: 24, lineHeight: 30, letterSpacing: -0.2 },
  displayM: { fontFamily: fontFamily.display, fontSize: 19, lineHeight: 25 },
  bodyL: { fontFamily: fontFamily.body, fontSize: 16, lineHeight: 23 },
  bodyM: { fontFamily: fontFamily.body, fontSize: 14, lineHeight: 20 },
  bodyS: { fontFamily: fontFamily.body, fontSize: 12.5, lineHeight: 17 },
  label: { fontFamily: fontFamily.bodySemiBold, fontSize: 12, lineHeight: 15, letterSpacing: 0.6 },
  button: { fontFamily: fontFamily.bodySemiBold, fontSize: 15.5, lineHeight: 19, letterSpacing: 0.2 },
  ledgerNumber: { fontFamily: fontFamily.mono, fontSize: 22, lineHeight: 26 },
  ledgerNumberL: { fontFamily: fontFamily.mono, fontSize: 32, lineHeight: 36 },
};

// Actual font loading happens in App.tsx via the @expo-google-fonts
// packages (see useFonts call there) — this file only defines the family
// name mapping consumed by `type` above.
