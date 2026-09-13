import {getTheme} from '@/shell/theme/get-theme';
import {productPage} from '@/shell/routes/product';

export {generateMetadata} from '@/shell/routes/product';
export default productPage(getTheme);
