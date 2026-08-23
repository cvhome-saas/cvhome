/**
 * ECharts module registration.
 *
 * Imported for its side effect by the chart host. Registering only the pieces the console
 * actually draws keeps the library tree-shaken — pulling the `echarts` barrel instead
 * would drag in every chart type, coordinate system and component.
 *
 * Add a module here when a new chart type needs it; nothing else should import from
 * `echarts/*` directly.
 */
import * as echarts from 'echarts/core';
import {BarChart, LineChart, PieChart} from 'echarts/charts';
import {GridComponent, TooltipComponent} from 'echarts/components';
import {LabelLayout} from 'echarts/features';
import {CanvasRenderer} from 'echarts/renderers';

echarts.use([BarChart, LineChart, PieChart, GridComponent, TooltipComponent, LabelLayout, CanvasRenderer]);

export {echarts};
export type {EChartsCoreOption} from 'echarts/core';
