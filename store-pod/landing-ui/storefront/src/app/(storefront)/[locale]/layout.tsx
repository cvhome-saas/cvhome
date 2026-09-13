import '@/app/globals.css';
import {getTheme} from '@/shell/theme/get-theme';
import {storefrontLayout} from '@/shell/routes/layout';

export {generateMetadata} from '@/shell/routes/layout';
export default storefrontLayout(getTheme);
