import {getTheme} from '@/shell/theme/get-theme';
import {loginPage} from '@/shell/routes/login';

export {generateMetadata} from '@/shell/routes/login';
export default loginPage(getTheme);
