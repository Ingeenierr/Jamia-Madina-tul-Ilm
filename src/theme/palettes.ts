/**
 * Two palettes sharing the same key names, so every component just asks
 * "what's textPrimary right now" via useTheme() instead of caring which
 * mode is active.
 *
 * Light = "Illuminated Ledger, daytime" — warm manuscript cream paper.
 * Dark  = "Illuminated Ledger, night" — the dusk-header ink extended to
 *         the whole app, gold leaf turned up slightly to keep contrast.
 */

export const lightColors = {
  inkGreen: '#013220',
  inkGreenLight: '#0E4A30',
  palm: '#1B5E20',
  palmLight: '#2E7D32',

  bgPaper: '#FCF5E5',
  bgPaperDim: '#F3EAD4',
  surface: '#FFFDF8',

  gold: '#D4AF37',
  goldDeep: '#996515',
  goldSoft: 'rgba(212, 175, 55, 0.16)',

  statusApproved: '#2E7D32',
  statusPending: '#E0A527',
  statusRejected: '#C24A3F',

  textOnDark: '#FCF5E5',
  textOnDarkMuted: 'rgba(252, 245, 229, 0.68)',
  textPrimary: '#122019',
  textSecondary: '#4B564E',
  textMuted: '#8A9490',

  hairline: 'rgba(212, 175, 55, 0.28)',
  hairlineDark: 'rgba(252, 245, 229, 0.14)',
  glassSurface: 'rgba(255, 253, 248, 0.72)',
  glassDark: 'rgba(1, 50, 32, 0.55)',
  shadow: '#01140D',

  paperGradient: ['#FFFDF8', '#F3EAD4'] as const,
  duskGradient: ['#013220', '#04211a', '#0a1712'] as const,
  goldLeafGradient: ['#D4AF37', '#996515'] as const,
  candleGlow: ['rgba(212,175,55,0.35)', 'rgba(212,175,55,0)'] as const,

  isDark: false,
};

export const darkColors = {
  inkGreen: '#013220',
  inkGreenLight: '#0E4A30',
  palm: '#2E7D32',
  palmLight: '#43A047',

  bgPaper: '#081C13',
  bgPaperDim: '#0C241A',
  surface: '#0F2A1E',

  gold: '#E3BE4E',
  goldDeep: '#C99A3D',
  goldSoft: 'rgba(227, 190, 78, 0.14)',

  statusApproved: '#4CAF50',
  statusPending: '#E6B84C',
  statusRejected: '#E0655A',

  textOnDark: '#FCF5E5',
  textOnDarkMuted: 'rgba(252, 245, 229, 0.68)',
  textPrimary: '#F2ECDD',
  textSecondary: 'rgba(242, 236, 221, 0.72)',
  textMuted: 'rgba(242, 236, 221, 0.48)',

  hairline: 'rgba(227, 190, 78, 0.22)',
  hairlineDark: 'rgba(252, 245, 229, 0.10)',
  glassSurface: 'rgba(20, 43, 32, 0.62)',
  glassDark: 'rgba(4, 20, 14, 0.68)',
  shadow: '#000000',

  paperGradient: ['#0C241A', '#071A12'] as const,
  duskGradient: ['#04140D', '#02100A', '#010A06'] as const,
  goldLeafGradient: ['#E3BE4E', '#B78A34'] as const,
  candleGlow: ['rgba(227,190,78,0.28)', 'rgba(227,190,78,0)'] as const,

  isDark: true,
};

export type Palette = typeof lightColors | typeof darkColors;
