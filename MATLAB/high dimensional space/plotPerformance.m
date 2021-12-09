
h = figure;
hold on;
title(strcat('Query Efficiency: Hilbert and Hyperbolic, MHT'),'FontSize',20);
xlabel('Space and Dimension','FontSize',16);
ylabel('%ge of maximum distance calcualtions','FontSize',16)

axes = gca;
axes.XTick = [1,2,3,4,5];
% axes.YTick = [1,2,4,8,16,32,64];
axes.XTickLabel = {'euc6','euc8','euc10','euc12','euc14'};
set(gca,'FontSize',14);

plot(performanceData(:,6),'--','Color',[0,0,0]);
plot(performanceData(:,5),':','Color',[0,0,0]);
plot(performanceData(:,4),'-','Color',[0,0,0]);


plot(performanceData(:,12),'--','Color',[1,0,0]);
plot(performanceData(:,11),':','Color',[1,0,0]);
plot(performanceData(:,10),'-','Color',[1,0,0]);



legend({'threshold t_{16}, Hyp','threshold t_4, Hyp','threshold t_1, Hyp','threshold t_{16}, Hilbert','threshold t_4, Hilbert','threshold t_1, Hilbert'},'FontSize',14,'Location','northwest');
hold off;

saveas(h,strcat('mhtPerformance.png'));

