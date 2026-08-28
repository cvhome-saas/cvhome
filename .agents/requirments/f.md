reframe this requirments


we want to go live with @store-core/console-ui/ as it will replace @store-core/seller-ui/ we already applyed layout , basic structure , some shared ui , but main goal is to be fully api call , not like now  
mocked data , we already have all api calls for seller-ui @store-core/seller-ui/projects/ which hold all http calls logic service , model ,so start from landing page marketing to fully have same funcnality  
like seller-ui , the new design might have a blocks not have equivlant http calls (backend) so we can mark it as todo in code with a place holder so we can implment later,note what need a fueature           
implmentation in lessons.md so later backend will implment later , during this big migration which will migrate all modules in steps , every module,page will have its planning phase , implment phase every   
phase is a one commit, test using chrome , you can run both old and new in two tabs as current backend support running both at same and access them using   old seller-ui.gateway.com:8000 new                 
console-ui.gateway.com:8000  the main template for console-ui here in @store-core/console-template which contains the full static html design that has many ideas about how new console should looks like , componants idea how it will be  
don't start planing all modules in one now , we will ask you later as every module might need a new compnants a new cases just define how every module planing how it will be