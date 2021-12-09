
h = figure;
hold on;
title(strcat('Exclusion Power Improvement: Hilbert over Hyperbolic'),'FontSize',20);
xlabel('Space and Dimension','FontSize',16);
ylabel('Improvement','FontSize',16)

axes = gca;
axes.XTick = [1,2,3,4,5];
% axes.YTick = [1,2,4,8,16,32,64];
axes.XTickLabel = {'euc6','euc8','euc10','euc12','euc14'};
set(gca,'FontSize',14);

plot(cosHypEucImprovement(:,3),'--','Color',[0,0,0]);
plot(cosHypEucImprovement(:,2),':','Color',[0,0,0]);
plot(cosHypEucImprovement(:,1),'-','Color',[0,0,0]);

legend({'threshold t_{16}','threshold t_4','threshold t_1'},'FontSize',14);
hold off;

saveas(h,strcat('cosHypEucImprovement.png'));

h2 = figure;
hold on;
title(strcat('Exclusive Power Improvement: Hilbert over Hyperbolic'),'FontSize',20);
xlabel('Space and Dimension','FontSize',16);
ylabel('Improvement','FontSize',16)


axes = gca;
axes.XTick = [1,2,3,4,5];
% axes.YTick = [1,2,4,8,16,32,64];
axes.XTickLabel = {'jsd6','jsd8','jsd10','jsd12','jsd14'};
set(gca,'FontSize',14);

plot(cosHypJsdImprovement(:,3),'--','Color',[0,0,0]);
plot(cosHypJsdImprovement(:,2),':','Color',[0,0,0]);
plot(cosHypJsdImprovement(:,1),'-','Color',[0,0,0]);

legend({'threshold t_{16}','threshold t_4','threshold t_1'},'FontSize',14);
hold off;

saveas(h2,strcat('cosHypJsdImprovement.png'));
