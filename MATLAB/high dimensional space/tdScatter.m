

predictor = 'Tri';
numberOfData = 50;
perms = (numberOfData * (numberOfData - 1))/2;

triAndJsdDists = zeros(perms,2);
pointer = 1;
for i = 1 : numberOfData - 1
    for j = i + 1 : numberOfData
        if predictor == 'Cos'
            triAndJsdDists(pointer,1) = csd(data(i,:),data(j,:));
        else
            triAndJsdDists(pointer,1) = tri(data(i,:),data(j,:));
        end
        triAndJsdDists(pointer,2) = jsd(data(i,:),data(j,:));
        pointer = pointer + 1;
    end
end

plotTitle = ' data';

factor = sqrt(2 * log(2));

h = figure;
hold on;

title(strcat(dataSeriesName,' Jsd vs {}',predictor),'FontSize',36);
xlabel(strcat(predictor,' distance'),'FontSize',20)
ylabel('Jsd distance','FontSize',20);

scatter(triAndJsdDists(:,1),triAndJsdDists(:,2));

plot(linspace(0,1,100),linspace(0,1,100));
plot(linspace(0,1,100),linspace(0,1/factor,100));

axes = gca;

set(gca,'FontSize',14);

saveas(h,strcat(predictor,'_scatter_td.png'));

hold off;