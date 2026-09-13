import {getTheme} from '@/shell/theme/get-theme';
import {checkoutPage} from '@/shell/routes/checkout';

export {generateMetadata} from '@/shell/routes/checkout';
export default checkoutPage(getTheme);
