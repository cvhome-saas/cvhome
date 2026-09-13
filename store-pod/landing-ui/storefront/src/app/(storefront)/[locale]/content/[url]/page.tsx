import {getTheme} from '@/shell/theme/get-theme';
import {contentPage} from '@/shell/routes/content';

export {generateMetadata} from '@/shell/routes/content';
export default contentPage(getTheme);
