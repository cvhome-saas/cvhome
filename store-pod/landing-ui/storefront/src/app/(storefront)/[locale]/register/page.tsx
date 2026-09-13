import {getTheme} from '@/shell/theme/get-theme';
import {registerPage} from '@/shell/routes/register';

export {generateMetadata} from '@/shell/routes/register';
export default registerPage(getTheme);
