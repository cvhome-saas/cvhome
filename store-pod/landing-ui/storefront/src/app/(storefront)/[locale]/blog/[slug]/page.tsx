import {getTheme} from '@/shell/theme/get-theme';
import {blogPostPage} from '@/shell/routes/blog-post';

export {generateMetadata} from '@/shell/routes/blog-post';
export default blogPostPage(getTheme);
