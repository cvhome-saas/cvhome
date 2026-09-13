import {getTheme} from '@/shell/theme/get-theme';
import {helpPage} from '@/shell/routes/help';

export {generateMetadata} from '@/shell/routes/help';
export default helpPage(getTheme);
