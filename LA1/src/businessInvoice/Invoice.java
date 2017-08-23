package businessInvoice;

public class Invoice {

	private int invoiceNo;
	private int balanceDue;
	private int year;
	private int month;
	private int day;
	
	public Invoice(int invoiceNo, int balanceDue, int year, int month, int day){
		
		this.balanceDue = balanceDue;
		this.year = year;
		this.month = month;
		this.day = day;
		
		if(invoiceNo<1000){
			this.invoiceNo = 0;
		}else{
			this.invoiceNo = invoiceNo;
		}
	}
	
}
