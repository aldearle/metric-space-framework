
h = figure;
hold on;
title(strcat('Query Efficiency: Improvement Ratio'),'FontSize',18);
xlabel('Space and Dimension','FontSize',16);
ylabel('Improvement Factor for Hilbert Exclusion','FontSize',16)

axes = gca;
ylim([1,2.2]);
axes.XTick = [1,2,3,4,5];
axes.XTickLabel = {'euc6','euc8','euc10','euc12','euc14'};
set(gca,'FontSize',14);


plot(performanceData(:,3)./performanceData(:,9),'--','Color',[0,0,0]);
plot(performanceData(:,2)./performanceData(:,8),':','Color',[0,0,0]);
plot(performanceData(:,1)./performanceData(:,7),'-','Color',[0,0,0]);

plot(performanceData(:,6)./performanceData(:,12),'--','Color',[1,0,0]);
plot(performanceData(:,5)./performanceData(:,11),':','Color',[1,0,0]);
plot(performanceData(:,4)./performanceData(:,10),'-','Color',[1,0,0]);

legend({'threshold t_{16}, GHT','threshold t_4, GHT','threshold t_1, GHT','threshold t_{16}, MHT','threshold t_4, MHT','threshold t_1, MHT'},'FontSize',14,'Location','northwest');
hold off;

saveas(h,strcat('queryImprovement.png'));

