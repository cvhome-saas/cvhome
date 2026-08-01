/** Nebular declares `NbJSThemeVariable.echarts` as a loose, effectively
 *  untyped bag of chart-theme colors. Narrow local shape covering only the
 *  fields the statistic components actually read. */
export interface NbEchartsTheme {
  bg: string;
  textColor: string;
  itemHoverShadowColor: string;
}

/** Loose local shape for the subset of ECharts `EChartsOption` these
 *  statistic components build. Not the library's own type — kept minimal
 *  and structural on purpose. */
export interface EChartsLikeOption {
  backgroundColor?: string;
  legend?: Record<string, unknown>;
  grid?: Record<string, unknown>;
  dataset?: Record<string, unknown>;
  xAxis?: Record<string, unknown> | Record<string, unknown>[];
  yAxis?: Record<string, unknown> | Record<string, unknown>[];
  series?: Record<string, unknown>[];
  tooltip?: Record<string, unknown>;
}
