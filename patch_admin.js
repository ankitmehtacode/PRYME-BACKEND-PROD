const fs = require('fs');
const file = '/Users/manishmehta/Documents/PRYME-BACKEND-PROD/Pryme-Frontend/src/pages/AdminDashboard.tsx';
let data = fs.readFileSync(file, 'utf8');

const queries = `
  const { data: banks = [], refetch: refetchBanks } = useQuery({
    queryKey: ["admin_banks"],
    queryFn: () => PrymeAPI.getAdminBanks().then(res => res.data || res),
    enabled: activeTab === "banks"
  });

  const { data: products = [], refetch: refetchProducts } = useQuery({
    queryKey: ["admin_products"],
    queryFn: () => PrymeAPI.getAdminProducts().then(res => res.data || res),
    enabled: activeTab === "offers"
  });

  const toggleBankMutation = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) => PrymeAPI.toggleBankVisibility(id, active),
    onSuccess: () => { toast({ title: "Bank Updated" }); refetchBanks(); }
  });
`;

data = data.replace('const { data: users = [] } = useQuery({', queries + '\n  const { data: users = [] } = useQuery({');

const banksTab = `
              {/* PARTNER BANKS TAB */}
              {activeTab === "banks" && (
                <div className="bg-[#0d0d14] rounded-2xl border border-white/[0.06] overflow-hidden animate-in fade-in slide-in-from-bottom-2">
                  <div className="p-4 border-b border-white/[0.06] flex justify-between items-center bg-white/[0.02]">
                    <h3 className="font-semibold text-white">Partner Bank Network</h3>
                    <Button size="sm" className="bg-blue-600 hover:bg-blue-700 text-white"><Plus className="w-4 h-4 mr-2" /> Add Bank</Button>
                  </div>
                  <table className="w-full text-left border-collapse">
                    <thead className="bg-white/[0.02] border-b border-white/[0.04]"><tr className="text-xs uppercase tracking-wider text-slate-500 font-semibold"><th className="px-6 py-4">Bank Name</th><th className="px-6 py-4">Logo URL</th><th className="px-6 py-4">Status</th><th className="px-6 py-4 text-right">Actions</th></tr></thead>
                    <tbody className="divide-y divide-white/[0.04] text-sm">
                      {banks.map((b: any) => (
                        <tr key={b.id} className="hover:bg-white/[0.03] transition-colors">
                          <td className="px-6 py-4 font-semibold text-white">{b.bankName}</td>
                          <td className="px-6 py-4 text-slate-500 text-xs truncate max-w-[200px]">{b.logoUrl || "No Logo"}</td>
                          <td className="px-6 py-4">
                            <button onClick={() => toggleBankMutation.mutate({ id: b.id, active: !b.active })} className={cn("px-2 py-1 text-xs font-medium rounded-md border", b.active ? "bg-green-500/15 text-green-400 border-green-500/25" : "bg-slate-500/15 text-slate-400 border-slate-500/25")}>
                              {b.active ? "Active" : "Inactive"}
                            </button>
                          </td>
                          <td className="px-6 py-4 text-right"><Button variant="ghost" size="sm" className="text-blue-400 hover:text-blue-300">Edit</Button></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
`;

const offersTab = `
              {/* OFFERS TAB */}
              {activeTab === "offers" && (
                <div className="bg-[#0d0d14] rounded-2xl border border-white/[0.06] overflow-hidden animate-in fade-in slide-in-from-bottom-2">
                  <div className="p-4 border-b border-white/[0.06] flex justify-between items-center bg-white/[0.02]">
                    <h3 className="font-semibold text-white">Dynamic Hero Offers</h3>
                    <Button size="sm" className="bg-blue-600 hover:bg-blue-700 text-white"><Plus className="w-4 h-4 mr-2" /> Add Offer</Button>
                  </div>
                  <table className="w-full text-left border-collapse">
                    <thead className="bg-white/[0.02] border-b border-white/[0.04]"><tr className="text-xs uppercase tracking-wider text-slate-500 font-semibold"><th className="px-6 py-4">Lender</th><th className="px-6 py-4">Campaign</th><th className="px-6 py-4">ROI</th><th className="px-6 py-4">Processing Fee</th><th className="px-6 py-4 text-right">Actions</th></tr></thead>
                    <tbody className="divide-y divide-white/[0.04] text-sm">
                      {products.map((p: any) => (
                        <tr key={p.id} className="hover:bg-white/[0.03] transition-colors">
                          <td className="px-6 py-4 font-semibold text-white">{p.lenderName || "Unknown"}</td>
                          <td className="px-6 py-4 text-slate-300">{p.campaignName || p.loanType}</td>
                          <td className="px-6 py-4 font-mono text-amber-400">{p.roi}%</td>
                          <td className="px-6 py-4 font-mono text-blue-400">{p.processingFee}%</td>
                          <td className="px-6 py-4 text-right"><Button variant="ghost" size="sm" className="text-blue-400 hover:text-blue-300">Edit</Button></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
`;

data = data.replace(
  '{/* USERS TAB */}',
  banksTab + '\n\n' + offersTab + '\n\n              {/* USERS TAB */}'
);

fs.writeFileSync(file, data);
