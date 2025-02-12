export class User {

    public userId!: number;
    public email: string | undefined;
    public userid!: number;
    public dempoid: string = '';
    public status: string = '';
    public active?: boolean | null;
    public corehours?: number | null;
    public employmenttype?: number | null;
    public displayName: string | null = null;
    public userName: string | null = null;
    public projectName: string | null = null;
    public fname: string = '';
    public lname: string = '';
    public permstartdate?: Date | null;
    public permenddate?: Date | null;
    public tempstartdate?: Date | null;
    public tempenddate?: Date | null;
    public uinit: string = '';
    public role?: number | null = 0;
    public schedulinglevel?: number | null = 0;
    public scheduledisplay?: number | null = 0;
    public title: string = '';
    public defaultproject?: number | null = 0;
    public phonenumber: string = '';
    public phonenumber1: string = '';
    public phonenumber2: string = '';
    public emailaddr: string = '';
    public emailaddr1: string = '';
    public bypasslockout?: number | null;
    public aislenumber?: number | null;
    public teamgroup: string = '';
    public workgroup: string = '';
    public cubeoffice?: number | null;
    public spanish?: number | null;
    public language: string = '';
    public canedit?: boolean | null = undefined;
    public trainedon: string = '';
    public citidate?: Date | null;
    public introemail?: boolean | null;
    public introemaildate?: Date | null;
    public facetofacedate?: Date | null;
    public welcomeemail?: boolean | null;
    public welcomeemaildate?: Date | null;
    public orientationdate?: Date | null;
    public referral: string = '';
    public tempagency: string = '';
    public userImage: string | null = null;
    public notes: string = '';
    public emercontactnumber: string = '';
    public emercontactnumber2: string = '';
    public emercontactname: string = '';
    public emercontactname2: string = '';
    public emercontactrel: string = '';
    public emercontactrel2: string = '';
    public dob?: Date | null;
    public state: string = '';
    public city: string = '';
    public zipcode: string = '';
    public homeaddress: string = '';
    public phonenumber3: string = '';
    public hiatusstartdate?: Date | null;
    public computernumber: string = '';
    public badgeidnumber: string = '';
    public acdagentnumber: string = '';
    public uniqueid: string = '';
    public preferredfname: string = '';
    public preferredlname: string = '';
    public entryFormName: string = '';
    public buddy?: boolean = false;
    public trainedOnArray!: string[];
    public entryDt?: Date | null;
    public entryBy: string = '';
    public modDt?: Date | null;
    public modBy: string = '';
    public canEdit?: boolean;
    public selected?: boolean;
    public checked?: boolean;
    public changed?: boolean;
    public empstatus: string = ''
    public manager: string = ''
    public hiatuscomments: string = ''
    public phonenumber4: string = '';
    public certification?: Date | null;
    public miscellaneous?: Date | null;
    public certnotes?: string = '';
    constructor() {
        this.cubeoffice = 0;
        this.facetofacedate = undefined;
        this.orientationdate = undefined;
        this.permstartdate = undefined;
        this.permenddate = undefined;
        this.tempstartdate = undefined;
        this.tempenddate = undefined;
        this.certification = undefined;
        this.miscellaneous = undefined;
        this.dob = undefined;
     }

}