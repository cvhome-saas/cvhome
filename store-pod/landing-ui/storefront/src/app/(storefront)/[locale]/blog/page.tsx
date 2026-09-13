import {getTheme} from '@/shell/theme/get-theme';
import {blogIndexPage} from '@/shell/routes/blog-index';

export {generateMetadata} from '@/shell/routes/blog-index';
export default blogIndexPage(getTheme);
