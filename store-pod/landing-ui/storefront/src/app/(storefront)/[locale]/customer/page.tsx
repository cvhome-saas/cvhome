import {getTheme} from '@/shell/theme/get-theme';
import {customerPage} from '@/shell/routes/customer';

export {generateMetadata} from '@/shell/routes/customer';
export default customerPage(getTheme);
