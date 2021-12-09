
h = figure;
hold on;
title(strcat('Hilbert and Hyperbolic Exclusion Power'),'FontSize',20);
xlabel('Space and Dimension','FontSize',16);
ylabel('Exclusion Power','FontSize',16)

axes = gca;
axes.XTick = [1,2,3,4,5];
% axes.YTick = [1,2,4,8,16,32,64];
axes.XTickLabel = {'euc6','euc8','euc10','euc12','euc14'};
set(gca,'FontSize',14);


plot(hypPower(:,1),'-','Color',[0,0,0]);
plot(hypPower(:,2),':','Color',[0,0,0]);
plot(hypPower(:,3),'--','Color',[0,0,0]);

plot(cosPower(:,1),'-','Color',[1,0,0]);
plot(cosPower(:,2),':','Color',[1,0,0]);
plot(cosPower(:,3),'--','Color',[1,0,0]);


legend({'Hyperbolic, threshold t_{1}','Hyperbolic, threshold t_4','Hyperbolic, threshold t_{16}','Hilbert, threshold t_{1}','Hilbert, threshold t_4','Hilbert, threshold t_{16}'},'FontSize',14);
hold off;

saveas(h,strcat('cosExcPower.png'));

