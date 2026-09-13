import {getTheme} from '@/shell/theme/get-theme';
import {searchPage} from '@/shell/routes/search';

export {generateMetadata} from '@/shell/routes/search';
export default searchPage(getTheme);
