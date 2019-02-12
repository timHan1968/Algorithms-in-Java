A Java implementation of the GS algorithm for stable matching. 


How to Run:

Run "java Framework txt1 txt2" where [txt1] contains information on each party's preference for the other while [txt2] is the file where the matched pairs would be written to. 


Format for [txt1]:
- An intger 'n' on the first line indicating size of both parties. 
- The 2nd to (n+1)th row represents applicant 1~n's preference on the employers (first employer on the row is most preferred).
- The (n+2)th to (2n+1)th row represents employer 1~n's preference on the applicant (first applicant on the row is most preferred).

Format for [txrt2]:
- Each row represents a paired match in the stable matching
- Left column corresponds to the employer
- Right column corresponds to the applicant