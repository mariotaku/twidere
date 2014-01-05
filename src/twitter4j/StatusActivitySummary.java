package twitter4j;

public interface StatusActivitySummary extends TwitterResponse {

	public IDs getFavoriters();

	public long getFavoritersCount();

	public IDs getRepliers();

	public long getRepliersCount();

	public IDs getRetweeters();

	public long getRetweetersCount();

}
