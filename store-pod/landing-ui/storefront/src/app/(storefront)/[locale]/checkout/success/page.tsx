import {getTheme} from '@/shell/theme/get-theme';
import {checkoutResultPage} from '@/shell/routes/checkout';

export {generateMetadata} from '@/shell/routes/checkout';
export default checkoutResultPage(getTheme, 'success');
