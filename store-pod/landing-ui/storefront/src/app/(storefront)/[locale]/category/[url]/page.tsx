import {getTheme} from '@/shell/theme/get-theme';
import {categoryPage} from '@/shell/routes/category';

export {generateMetadata} from '@/shell/routes/category';
export default categoryPage(getTheme);
