import {getTheme} from '@/shell/theme/get-theme';
import {orderPage} from '@/shell/routes/order';

export {generateMetadata} from '@/shell/routes/order';
export default orderPage(getTheme);
